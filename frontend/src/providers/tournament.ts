import * as assert from 'assert';
import * as _ from 'lodash';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Headers } from '@angular/http';
import { RequestOptions } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

const TOURNAMENT_URL = '/api/tournament';
const RUNNING_TOURNAMENT_URL = '/api/tournament/running';

export enum MatchState {
  RUNNING,
  FINISHED,
}

export class Player {
  readonly id: string;
  readonly name: string;
  readonly avatarImage: string;

  constructor(
    data: any,
  ) {
    this.id = data.id;
    this.name = data.name;
    this.avatarImage = data.avatarImage;
  }
}

enum InternalTeamName {
  teamA,
  teamB,
}

export class Team {
  readonly players: Player[];
  readonly name: string;

  constructor(
    public readonly internalName: InternalTeamName,
    data: any,
  ) {
    this.players = _(data)
      .pick(['player1', 'player2'])
      .map((player) => new Player(player))
      .value();
    this.name = data.name;
  }
}

export class Match {
  teamA: number;
  teamB: number;
  state: string;

  constructor(
    data: any,
  ) {
    this.teamA = data.teamA;
    this.teamB = data.teamB;
    this.state = data.state;
  }
}

class State {

  private readonly stringyfiedState: string;

  constructor(
    state: any,
  ) {
    this.stringyfiedState = JSON.stringify(state);
  }

  getState() {
    return JSON.parse(this.stringyfiedState);
  }
}

@Injectable()
export class Tournament {

  private updateInPrgoress: number = 0;
  private tournament: any;

  private undoStack: State[] = [];

  canUndo() {
    return !_.isEmpty(this.undoStack);
  }

  undo() {
    this.tournament = this.undoStack.pop().getState();
    this.push();
  }

  recordState() {
    this.undoStack.push(new State(this.tournament));
  }

  constructor(
    public http: Http,
  ) {
   //
 }

  addGoal(team: string): Promise<any> {
    return this.get()
      .then(tournament => {
        this.recordState();
        const running = Tournament.findRunning(tournament.matches)
        running[team] += 1;
        this.push();
      });
  }

  getRunningMatch(): Promise<Match> {
    return this.get()
      .then(tournament => {
        if (!tournament) {
          return;
        }
        const running = Tournament.findRunning(tournament.matches);
        if (!running) {
          return;
        }
        return new Match(running);
      });
  }

  getWinner(match) {
    if (!match) {
      return;
    }
    if (match.teamA >= 6) {
      return new Team(InternalTeamName.teamA, this.tournament.teamA);
    }
    if (match.teamB >= 6) {
      return new Team(InternalTeamName.teamB, this.tournament.teamB);
    }
  }

  getTeams(): Promise<Team[]> {
    return this.get()
      .then(tournament => [
        new Team(InternalTeamName.teamA, tournament.teamA),
        new Team(InternalTeamName.teamB, tournament.teamB),
      ]);
  }

  isTournamentFinished() {
    return this.get()
      .then(tournament => {

      });
  }

  getBestOfN() {
    return this.get()
      .then((tournament) => tournament.bestOfN);
  }

  reset(tournament) {
    this.recordState();
    this.push();
  }

  newMatch(): Promise<void> {
    return this.get()
      .then(tournament => this.http
        .post(TOURNAMENT_URL + '/' + tournament.id, '')
        .toPromise()
      )
      .then(() => this.pull());
  }

  getWins() {
    return this.get()
      .then((tournament) => this.countWins(_.filter(tournament.matches, {state: MatchState[MatchState.FINISHED]})));
  }

  countWins(matches) {
    return _.reduce(matches, (memo, match) => {
      const winner = this.getWinner(new Match(match));
      memo[InternalTeamName[winner.internalName]] += 1;
      return memo;
    }, {teamA: 0, teamB: 0});
  }

  finishMatch() {
    return this.get()
      .then((tournament) => {
        const running = Tournament.findRunning(tournament.matches);
        running.state = MatchState[MatchState.FINISHED];

        assert(!Tournament.findRunning(tournament.matches));
        const wins = this.countWins(tournament.matches);
        const previousBestOfN = this.tournament.bestOfN;
        this.tournament.bestOfN = _(wins).values().max() * 2 + 1;
        return [new Match(running), this.tournament.bestOfN > previousBestOfN];
      })
      .then((args) => {
        this.push();
        return args;
      });
  }

  finishTournament(): Promise<string> {
    return this.get()
      .then((tournament) => {
        this.tournament.state = MatchState[MatchState.FINISHED];
      })
      .then((args) => {
        this.push();
        return args;
      });

  }

  cancelMatch(): Promise<string> {
    console.error('Tournament.cancelMatch Not yet implemented!');
    return Promise.resolve(JSON.stringify(this.tournament));
  }

  swapTeams(): Promise<void> {
    return this.get()
      .then(tournament => [
        tournament.teamA, tournament.teamB,
      ] = [
        tournament.teamB, tournament.teamA,
      ])
      .then(() => {this.push()});
  }

  getUpdateInProgress() {
    return this.updateInPrgoress > 0;
  }

  private static findRunning(matches) {
    return _.find(matches, {state: MatchState[MatchState.RUNNING]});
  }

  private get(): Promise<any> {
    if (this.tournament) {
      return Promise.resolve(this.tournament);
    }

    return this.pull()
      .then(() => this.tournament);
   }

  private pull(): Promise<any> {
    return this.http.get(RUNNING_TOURNAMENT_URL)
      .map(res => res.json())
      .toPromise()
      .then(tournament => {
        this.tournament = tournament;
      });
  }

  private push(): Promise<any> {
    this.updateInPrgoress += 1;
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    return this.http.put(TOURNAMENT_URL, this.tournament, options)
      .toPromise()
      .then(() => this.updateInPrgoress -= 1);
   }
}
