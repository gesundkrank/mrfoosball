export class Player {
  id: String;
  name: String;
  avatarImage: String;
}

export class Team {
  player1: Player;
  player2: Player;
}

export enum State {
  RUNNING,
  FINISHED,
}

export class Match {
  teamA: number;
  teamB: number;
  state: State;
}

export class Tournament {
  id: number;
  bestOfN: number;
  teamA: Team;
  teamB: Team;
  state: State;
  matches: Match[]
}
