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
import { NavController, NavParams, ToastController } from 'ionic-angular';
import { Logger, LoggingService } from 'ionic-logging-service';

import { ChannelService } from '../../../app/channel.service';
import { TournamentController } from '../../../controllers/tournamentController';
import { PlayerSkill } from '../../../models/playerSkill';
import { Tournament } from '../../../models/tournament';
import { IndexPage } from '../../index';

@Component({
             selector: 'page-playerstats',
             templateUrl: 'playerStats.html',
           })
export class PlayerStatsPage {

  private readonly logger: Logger;

  private id: string;
  stats: PlayerSkill[];

  constructor(public http: HttpClient,
              public toastController: ToastController,
              private readonly navCtrl: NavController,
              private readonly navParams: NavParams,
              private readonly tournamentController: TournamentController,
              private readonly loggingService: LoggingService,
              private channelService: ChannelService) {
    this.logger = this.loggingService.getLogger('StatsPage');
  }

  ionViewDidEnter() {
    this.id = this.channelService.channelId;
    this.loadPlayerStats();
  }

  handleError(error) {
    this.logger.error('handleError', 'Failed to load data: ', error);

    if (this.navCtrl.getActive().name !== IndexPage.name) {
      return this.navCtrl.push(IndexPage, error.error);
    }
  }

  loadPlayerStats() {
    this.http.get<PlayerSkill[]>('/api/stats/' + this.id)
      .subscribe(
        (stats: PlayerSkill[]) => this.stats = stats,
        error => this.handleError(error)
      );
  }

  roundSkill(skill: number) {
    return skill ? skill.toFixed(2) : '';
  }

  winRate(playerSkill: PlayerSkill): string {
    return ((playerSkill.wins / playerSkill.games) * 100).toFixed(2) + ' %';
  }

  addPlayer(player) {
    this.http.post('/api/tournament/' + this.id + '/queue', player)
      .subscribe(
        error => this.handleError(error)
      );
    this.presentToast('Adding ' + player.name + ' to the queue.');
  }

  private presentToast(message) {
    void this.toastController.create({
      message,
      duration: 2000,
      cssClass: 'queueToast',
    }).present();
  }
}
