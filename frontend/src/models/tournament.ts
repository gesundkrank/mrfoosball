export class Player {
  id: String;
  name: String;
  avatarImage: String;
  trueSkillMean: number;
  trueSkillStandardDeviation: number;
}

export class Team {
  player1: Player;
  player2: Player;

  rating() {
    return Math.round((this.player1.trueSkillMean - 3 * this.player1.trueSkillStandardDeviation) +
                      (this.player2.trueSkillMean - 3 * this.player2.trueSkillStandardDeviation))
  }
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
  matches: Match[];
  teamAPlayer1SkillChange: number;
  teamAPlayer2SkillChange: number;
  teamBPlayer1SkillChange: number;
  teamBPlayer2SkillChange: number;
}
