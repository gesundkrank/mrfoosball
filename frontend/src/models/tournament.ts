export interface Player {
  id: string;
  name: string;
  avatarImage: string;
  trueSkillMean: number;
  trueSkillStandardDeviation: number;
}

export class Team {
  player1: Player;
  player2: Player;

  rating() {
    return Math.round((this.player1.trueSkillMean - 3 * this.player1.trueSkillStandardDeviation) +
                      (this.player2.trueSkillMean - 3 * this.player2.trueSkillStandardDeviation));
  }
}

export enum State {
  RUNNING = 'RUNNING',
  FINISHED = 'FINISHED',
}

export interface Match {
  teamA: number;
  teamB: number;
  state: State;
}

export interface Tournament {
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
