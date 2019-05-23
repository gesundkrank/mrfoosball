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

import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { Logger, LoggingService } from 'ionic-logging-service';

import { ChannelService } from '../../../app/channel.service';
import { TournamentController } from '../../../controllers/tournamentController';
import { TeamStat } from '../../../models/teamStat';
import { IndexPage } from '../../index';

@Component({
             selector: 'page-teamstats',
             templateUrl: 'teamStats.html',
           })
export class TeamStatsPage {

  private readonly logger: Logger;

  private id: string;
  stats: TeamStat[];

  constructor(public http: HttpClient,
              private readonly navCtrl: NavController,
              private readonly navParams: NavParams,
              private readonly tournamentController: TournamentController,
              private readonly loggingService: LoggingService,
              private channelService: ChannelService) {
    this.logger = this.loggingService.getLogger('TeamStatsPage');
  }

  ionViewDidEnter() {
    this.id = this.channelService.channelId;
    this.loadTeamStats();
  }

  handleError(error) {
    this.logger.error('handleError', 'Failed to load data: ', error);

    if (this.navCtrl.getActive().name !== IndexPage.name) {
      return this.navCtrl.push(IndexPage, error.error);
    }
  }

  loadTeamStats() {
    this.http.get<TeamStat[]>('/api/stats/' + this.id + '/teams')
      .subscribe(
        (stats: TeamStat[]) => this.stats = stats,
        error => this.handleError(error)
      );
  }

  roundSkill(skill: number) {
    return skill ? skill.toFixed(2) : '';
  }

  winRate(teamStat: TeamStat): string {
    return ((teamStat.tournamentsWon / teamStat.tournamentsPlayed) * 100).toFixed(2) + ' %';
  }

}
