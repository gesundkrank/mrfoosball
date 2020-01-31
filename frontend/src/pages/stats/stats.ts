/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
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

import { TournamentController } from '../../controllers/tournamentController';
import { PlayerSkill } from '../../models/playerSkill';
import { Player, Tournament } from '../../models/tournament';
import { IndexPage } from '../index';
import { MatchPage } from '../match/match';

@Component({
             selector: 'page-stats',
             templateUrl: 'stats.html',
           })
export class StatsPage {

  private readonly logger: Logger;

  private id: string;
  queue: Player[];
  stats: PlayerSkill[];
  lastTournaments: Tournament[];

  constructor(public http: HttpClient,
              public toastController: ToastController,
              private readonly navCtrl: NavController,
              private readonly navParams: NavParams,
              private readonly tournamentController: TournamentController,
              private readonly loggingService: LoggingService) {
    this.logger = this.loggingService.getLogger('StatsPage');
  }

  ionViewDidEnter() {
    this.id = this.navParams.get('id');
    window.history.pushState(undefined, undefined, '/' + this.id);
    this.tournamentController.setId(this.id);
    this.checkTournament()
      .catch(err => this.handleError(err));
    this.loadPlayerStats();
    this.loadLastTournaments();
  }

  handleError(error) {
    this.logger.error('handleError', 'Failed to load data: ', error);

    if (this.navCtrl.getActive().name !== IndexPage.name) {
      return this.navCtrl.push(IndexPage, error.error);
    }
  }

  refresh(refresher) {
    return this.checkTournament(false)
      .then(() => refresher.complete());
  }

  addPlayer(player) {
    this.http.post('/api/tournament/' + this.id + '/queue', player)
      .subscribe(
        error => this.handleError(error)
      );
    this.presentToast('Adding ' + player.name + ' to the queue.');
    this.checkTournament(false)
      .catch(err => this.handleError(err));
  }

  removePlayer(player) {
    this.http.delete('/api/tournament/' + this.id + '/queue/' + player.id)
      .subscribe(
        error => this.handleError(error)
      );
    this.presentToast('Removing ' + player.name + ' from the queue.');
    this.checkTournament(false)
      .catch(err => this.handleError(err));
  }

  private presentToast(message) {
    void this.toastController.create({
      message,
      duration: 2000,
      cssClass: 'queueToast',
    }).present();
  }

  checkTournament(loop = true) {
    return this.tournamentController.getRunningMatch()
      .then(match => {
        if (match) {
          return this.newMatch();
        } else {
          this.loadQueue();
          if (loop) {
            setTimeout(() => this.checkTournament(), 5000);
          }
        }
      });
  }

  loadQueue() {
    this.http.get<Player[]>('/api/tournament/' + this.id + '/queue')
      .subscribe(
        (queue: Player[]) => this.queue = queue,
        error => this.handleError(error)
      );
  }

  loadPlayerStats() {
    this.http.get<PlayerSkill[]>('/api/stats/' + this.id)
      .subscribe(
        (stats: PlayerSkill[]) => this.stats = stats,
        error => this.handleError(error)
      );
  }

  loadLastTournaments() {
    this.http.get<Tournament[]>('/api/tournament/' + this.id + '?num=10')
      .subscribe(
        (lastTournaments: Tournament[]) => this.lastTournaments = lastTournaments,
        error => this.handleError(error)
      );
  }

  newMatch() {
    return this.navCtrl.push(MatchPage, { 'id': this.id });
  }

  roundSkill(skill: number) {
    return skill ? skill.toFixed(2) : '';
  }

  private wins(tournament: Tournament): { [key: string]: number } {
    return this.tournamentController.countWins(tournament.matches);
  }

  winsTeamA(tournament: Tournament): number {
    return this.wins(tournament)['teamA'];
  }

  winsTeamB(tournament: Tournament): number {
    return this.wins(tournament)['teamB'];
  }

  winRate(playerSkill: PlayerSkill): string {
    return ((playerSkill.wins / playerSkill.games) * 100).toFixed(2) + ' %';
  }

}
