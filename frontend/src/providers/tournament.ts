import * as assert from 'assert';
import * as _ from 'lodash';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Headers } from '@angular/http';
import { RequestOptions } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

const TOURNAMENT_URL = '/api/tournament';

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

enum TeamName {
  teamGrey,
  teamBlack,
}

export class Team {
  readonly players: Player[];

  constructor(
    public readonly name: TeamName,
    data: any,
  ) {
    this.players = _.map(data, (player) => new Player(player));
  }
}

export class Match {
  teamGrey: number;
  teamBlack: number;
  state: string;

  constructor(
    data: any,
  ) {
    this.teamGrey = data.teamGrey;
    this.teamBlack = data.teamBlack;
    this.state = data.state;
  }
}

@Injectable()
export class Tournament {

  private tournament: any;

  constructor(
    public http: Http,
  ) {
   //
 }

  addGoal(team: string): Promise<any> {
    let previousState;
    return this.get()
      .then(tournament => {
        previousState = JSON.stringify(tournament);
        const running = this.findRunning(tournament.matches)
        running[team] += 1;
      })
      .then(() => this.push())
      .then(() => previousState);
  }

  getRunningMatch(): Promise<Match> {
    return this.get()
      .then(tournament => {
        if (!tournament) {
          return;
        }
        const running = this.findRunning(tournament.matches);
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
    if (match.teamGrey >= 6) {
      return new Team(TeamName.teamGrey, this.tournament.teamGrey);
    }
    if (match.teamBlack >= 6) {
      return new Team(TeamName.teamBlack, this.tournament.teamBlack);
    }
  }

  getTeams(): Promise<Team[]> {
    return this.get()
      .then(tournament => [
        new Team(TeamName.teamGrey, tournament.teamGrey),
        new Team(TeamName.teamBlack, tournament.teamBlack),
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
    this.tournament = JSON.parse(tournament);
    return this.push();
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
      memo[TeamName[winner.name]] += 1;
      return memo;
    }, {teamGrey: 0, teamBlack: 0});
  }

  finishMatch() {
    return this.get()
      .then((tournament) => {
        const running = this.findRunning(tournament.matches);
        running.state = MatchState[MatchState.FINISHED];

        assert(!this.findRunning(tournament.matches));
        const wins = this.countWins(tournament.matches);
        const previousBestOfN = this.tournament.bestOfN;
        this.tournament.bestOfN = _(wins).values().max() * 2 + 1;
        return [new Match(running), this.tournament.bestOfN > previousBestOfN];
      })
      .then((args) => this.push().then(() => args));
  }

  endTournament(): Promise<string> {
    return this.get()
      .then((tournament) => {
        const state = JSON.stringify(this.tournament);
        this.tournament = null;
        return state;
      });
  }

  private findRunning(matches) {
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
    return this.http.get(TOURNAMENT_URL)
      .map(res => res.json())
      .toPromise()
      .then(tournament => {
        this.tournament = tournament;
      });
  }

  private push(): Promise<any> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    return this.http.put(TOURNAMENT_URL, this.tournament, options)
      .toPromise();
   }
}
