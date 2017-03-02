import * as _ from 'lodash';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Headers } from '@angular/http';
import { RequestOptions } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

const URL = 'http://localhost:8000/tournament.json';

enum MatchState {
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

export class Team {
  readonly players: Player[];

  constructor(
    data: any,
  ) {
    this.players = _.map(data, (player) => new Player(player));
  }
}

export class Match {
  teamA: number;
  teamB: number;

  constructor(
    data: any,
  ) {
    this.teamA = data.teamA;
    this.teamB = data.teamB;
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
        this.findRunning(tournament.matches)[team] += 1;
      })
      .then(() => this.push())
      .then(() => previousState);
  }

  getRunningMatch(): Promise<Match> {
    return this.get()
      .then(tournament => new Match(this.findRunning(tournament.matches)));
  }

  getTeams(): Promise<Team[]> {
    return this.get()
      .then(tournament => [
        new Team(tournament.teamA),
        new Team(tournament.teamB),
      ]);
  }

  reset(tournament) {
    this.tournament = JSON.parse(tournament);
    return this.push();
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
    return this.http.get(URL)
      .map(res => res.json())
      .toPromise()
      .then(tournament => {
        this.tournament = tournament;
      });
  }

  private push(): Promise<any> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    if (1 > 0) return Promise.resolve();
    return this.http.post(URL, this.tournament, options)
      .toPromise();
   }
}
