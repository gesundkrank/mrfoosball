/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
  trueSkillMean: number;
  trueSkillStandardDeviation: number;
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
