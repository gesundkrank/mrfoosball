import * as assert from 'assert';
import * as _ from 'lodash';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Headers } from '@angular/http';
import { RequestOptions } from '@angular/http';
import { ResponseOptions } from '@angular/http';
import { Response } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { MockBackend } from '@angular/http/testing';

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
  readonly color: string;

  constructor(
    public readonly internalName: InternalTeamName,
    data: any,
  ) {
    this.players = _(data)
      .pick(['player1', 'player2'])
      .map((player) => new Player(player))
      .value();
    this.name = data.name;
    this.color = data.color;
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

  private updateInProgress: number = 0;
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
    private backend: MockBackend,
  ) {
    this.setupMockBackend();
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
          if (tournament.state == MatchState[MatchState.RUNNING]) {
            return this.newMatch().then(tournament => this.getRunningMatch())
          }
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

  getBestOfN(): Promise<number> {
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

  cancelMatch(): Promise<void> {
    this.recordState();
    return this.http.delete(RUNNING_TOURNAMENT_URL)
      .toPromise();
  }

  getUpdateInProgress() {
    return this.updateInProgress > 0;
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
    this.updateInProgress += 1;
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    return this.http.put(TOURNAMENT_URL, this.tournament, options)
      .toPromise()
      .then(() => this.updateInProgress -= 1);
   }

  private mockDb = {
    "id": 1,
    "bestOfN": 1,
    "teamA": {
      "name": "grey",
      "color": "purple",
      "player1": {
        "id": "U12PAFQ9E",
        "name": "grey front",
        "avatarImage": "https://avatars.slack-edge.com/2016-04-22/36805531893_c20a75f7e3ffe6cf9d32_192.jpg",
      },
      "player2": {
        "id": "U15CRSYKY",
        "name": "grey back",
        "avatarImage": "https://secure.gravatar.com/avatar/f4bfdfbc97572182ad1bb871161dbe64.jpg?s=192&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0001-192.png",
      },
    },
    "teamB": {
      "name": "black",
      "color": "orange",
      "player1": {
        "id": "U12G6EUSZ",
        "name": "black front",
        "avatarImage": "https://avatars.slack-edge.com/2017-02-27/146734268195_f68a8b3c1fd4740a366b_192.jpg",
      },
      "player2": {
        "id": "U12GTAA49",
        "name": "black back",
        "avatarImage": "https://avatars.slack-edge.com/2016-04-21/36516684115_cdf0846b0c832973080b_192.jpg",
      },
    },
    "state": "RUNNING",
    "matches": [
      {
        "id": 2,
        "date": "2017-03-22T17:07:15.414Z",
        "teamA": 0,
        "teamB": 0,
        "state": "RUNNING",
        // "players": [{
        //   "id": "U12PAFQ9E",
        //   "color": "grey",
        //   "goals": 2,
        //   "ownGoals": 1,
        //   "position": "front",
        // }, {
        //   "id": "U15CRSYKY",
        //   "color": "grey",
        //   "goals": 2,
        //   "ownGoals": 0,
        //   "position": "back",
        // }, {
        //   "id": "U12G6EUSZ",
        //   "color": "black",
        //   "goals": 4,
        //   "ownGoals": 0,
        //   "position": "front",
        // }, {
        //   "id": "U12GTAA49",
        //   "color": "black",
        //   "goals": 0,
        //   "ownGoals": 0,
        //   "position": "back",
        // }],
      },
    ],
  };

  private setupMockBackend() {
    this.backend.connections.subscribe(c => {
      console.log(c);

      // GET: /api/tournament/queue -> return current queue
      if (c.request.url.match(/\/api\/tournament\/queue$/i) && c.request.method === 0) {
        let res = new Response(new ResponseOptions({
          body: JSON.stringify([])
        }));

        c.mockRespond(res);
        return;
      }
      // GET: /api/tournament -> return current tournament
      if (c.request.url.match(/\/api\/tournament\/running$/i) && c.request.method === 0) {
        let res = new Response(new ResponseOptions({
          body: JSON.stringify(this.mockDb)
        }));

        c.mockRespond(res);
        return;
      }
      // GET: /api/tournament -> return current tournament
      if (c.request.url.match(/\/api\/tournament$/i) && c.request.method === 0) {
        let res = new Response(new ResponseOptions({
          body: JSON.stringify(this.mockDb)
        }));

        c.mockRespond(res);
        return;
      }
      // PUT: /api/tournament -> update current tournament
      if (c.request.url.match(/\/api\/tournament$/i) && c.request.method === 2) {
        this.mockDb = c.request._body;
        setTimeout(() => {
          c.mockRespond(new Response(new ResponseOptions()));
        }, 500);
        return;
      }
      // GET: /api/tournament/:id -> return tournament with id
      let match = c.request.url.match(/\/api\/tournament\/[0-9]+$/i);
      if (match && c.request.method === 1) {
        this.mockDb.matches.push({
          id: (_.last(this.mockDb.matches).id as number) + 1,
          date: new Date().toISOString(),
          teamA: 0,
          teamB: 0,
          state: "RUNNING",
        });
        c.mockRespond(new Response(new ResponseOptions()));
        return;
      }
      // DELETE: /api/tournament/:id -> delete tournament with id
      if (match && c.request.method === 3) {
        this.mockDb.matches.pop();
        c.mockRespond(new Response(new ResponseOptions()));
        return;
      }
    });
  }
}
