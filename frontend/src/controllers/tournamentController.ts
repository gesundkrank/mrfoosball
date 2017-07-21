import _ from "lodash";
import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Headers} from "@angular/http";
import {RequestOptions} from "@angular/http";
import {URLSearchParams} from "@angular/http";
import "rxjs/add/operator/map";
import "rxjs/add/operator/toPromise";
import {Match} from "../models/tournament";
import {State} from "../models/tournament";
import {Team} from "../models/tournament";
import {Tournament} from "../models/tournament";

const TOURNAMENT_URL = '/api/tournament';
const RUNNING_TOURNAMENT_URL = '/api/tournament/running';

@Injectable()
export class TournamentController {

  private updateInProgress: number = 0;
  private tournament: Tournament;

  private undoStack: Tournament[] = [];

  canUndo() {
    return !_.isEmpty(this.undoStack);
  }

  undo() {
    this.tournament = this.undoStack.pop();
    this.push();
  }

  recordState() {
    this.undoStack.push(_.cloneDeep(this.tournament));
  }

  constructor(public http: Http) {
    //
  }

  addGoal(team: string): Promise<void> {
    return this.get()
      .then(() => {
        if (!team) {
          return;
        }

        this.recordState();
        const running = this.findRunning();
        if (!running || running[team] >= 6) {
          return;
        }
        running[team] += 1;
        return this.push();
      });
  }

  checkAndUpdateTournamentState(): Promise<State> {
    if (this.tournament.state == State.FINISHED) {
      return Promise.resolve(State.FINISHED);
    }
    const wins = this.countWins(this.tournament.matches);
    const maxWins = this.tournament.bestOfN / 2 + 1;

    if (Math.max(wins['teamA'], wins['teamB']) >= maxWins) {
      return this.finishTournament().then(() => this.tournament.state);
    }

    return Promise.resolve(this.tournament.state)
  }

  getRunningMatch(): Promise<Match> {
    return this.pull()
      .then(tournament => {
        if (!tournament) {
          return;
        }

        return this.checkAndUpdateTournamentState().then(state => {
          const running = this.findRunning();
          if (running) {
            return running;
          }

          if (state === State.RUNNING) {
            return this.newMatch().then(tournament => this.getRunningMatch())
          }
          return;
        });
      });
  }

  static getWinner(match: Match): string {
    if (!match) {
      return;
    }
    if (match.teamA >= 6) {
      return 'teamA';
    }
    if (match.teamB >= 6) {
      return 'teamB';
    }
  }

  getWinnerTeam(match): Promise<Team> {
    return this.get().then(tournament => {
      const winner = TournamentController.getWinner(match);
      if (!winner) {
        return;
      }
      return tournament[winner];
    })
  }

  getTeamNames(): Promise<{ [key: string]: string }> {
    return this.getWins().then(wins => {
      const matchCount = wins ? wins['teamA'] + wins['teamB'] : 0;
      if (matchCount % 2 == 0) {
        return {
          left: 'teamA',
          right: 'teamB',
        };
      }
      return {
        left: 'teamB',
        right: 'teamA',
      };
    });
  }

  getTeams(): Promise<{ [key: string]: Team }> {
    return this.get().then(tournament => _.pick(tournament, ['teamA', 'teamB']));
  }

  getBestOfN(): Promise<number> {
    return this.get()
      .then((tournament) => tournament.bestOfN);
  }

  newMatch(): Promise<void> {
    return this.get()
      .then(tournament => this.http
        .post(TOURNAMENT_URL + '/' + tournament.id, '')
        .toPromise()
      )
      .then(() => this.pull());
  }

  newTournament() {
    const headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    const options = new RequestOptions({headers: headers});

    const tournament = this.tournament;
    let data = new URLSearchParams();
    data.append('playerB1', tournament.teamA.player1.id.toString());
    data.append('playerB2', tournament.teamA.player2.id.toString());
    data.append('playerA1', tournament.teamB.player1.id.toString());
    data.append('playerA2', tournament.teamB.player2.id.toString());
    data.append('bestOfN', tournament.bestOfN.toString());

    return this.http.post(TOURNAMENT_URL, data, options).toPromise()
  }

  getWins() {
    return this.get().then(tournament => {
      return this.countWins(tournament.matches);
    });
  }

  countWins(matches: Match[]): { [key: string]: number } {
    return _.filter(matches, {state: State.FINISHED})
      .reduce((memo, match) => {
        const winner = TournamentController.getWinner(match);
        memo[winner] += 1;
        return memo;
      }, {teamA: 0, teamB: 0})
  }

  finishMatch(): Promise<boolean> {
    return this.get()
      .then((tournament) => {
        const running = this.findRunning();
        running.state = State.FINISHED;

        const wins = this.countWins(tournament.matches);
        const playedBestOfN = _(wins).values().max() * 2 - 1;
        return this.tournament.bestOfN == playedBestOfN;
      })
      .then((finished) => {
        this.push();
        return finished;
      });
  }

  finishTournament(): Promise<void> {
    return this.get()
      .then((tournament) => {
        tournament.state = State.FINISHED;
        return this.push();
      });
  }

  cancelMatch(): Promise<void> {
    this.recordState();
    return this.http.delete(RUNNING_TOURNAMENT_URL)
      .toPromise();
  }

  getUpdateInProgress() {
    return this.updateInProgress > 0;
  }

  private findRunning(): Match {
    return _.find(this.tournament.matches, {state: State.RUNNING});
  }

  private get(): Promise<Tournament> {
    if (this.tournament) {
      return Promise.resolve(this.tournament);
    }

    return this.pull();
  }

  private pull(): Promise<Tournament> {
    return this.http.get(RUNNING_TOURNAMENT_URL)
      .map(res => res.json())
      .toPromise()
      .then(tournament => {
        if (tournament) {
          tournament.state = State[tournament.state];
          tournament.matches.forEach(match => match.state = State[match.state]);
        }
        this.tournament = tournament;
        return tournament;
      });
  }

  private push(): Promise<void> {
    this.updateInProgress += 1;
    const headers = new Headers({'Content-Type': 'application/json'});
    const options = new RequestOptions({headers: headers});
    const tournament = _.cloneDeep(this.tournament);
    tournament.state = State[tournament.state];
    tournament.matches.forEach(match => match.state = State[match.state]);
    return this.http.put(TOURNAMENT_URL, tournament, options)
      .toPromise()
      .then(() => this.updateInProgress -= 1);
  }
}
