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

import { Component } from '@angular/core';
import * as Cookies from 'es-cookie';
import { AlertController, ModalController, NavController, ToastController } from 'ionic-angular';
import { Logger, LoggingService } from 'ionic-logging-service';

import { ChannelService } from '../../app/channel.service';
import { QRScannerPage } from '../qr-scanner/qr-scanner';
import { StatsPage } from '../stats/stats';

@Component({
             selector: 'page-index',
             templateUrl: 'index.html',
           })
export class IndexPage {
  private logger: Logger;

  constructor(private readonly navCtrl: NavController,
              private readonly alertCtrl: AlertController,
              private readonly modalCtrl: ModalController,
              private readonly toastCtrl: ToastController,
              private readonly loggingService: LoggingService,
              private channelService: ChannelService) {
    this.logger = this.loggingService.getLogger('IndexPage');
  }

  ionViewDidEnter() {
    const splits = location.search.substring(1).split('=');
    if (splits && splits.length === 2 && splits[0] === 'id') {
      const id = splits[1];
      this.channelService.updateCahnnelId(id);
      return this.goToStatsPage(id);
    } else if (Cookies.get('id') !== undefined) {
      const id = Cookies.get('id');
      this.channelService.updateCahnnelId(id);
      return this.goToStatsPage(id);
    } else {
      window.history.pushState(undefined, undefined, '/');

      const error = this.navCtrl.getActive().getNavParams().get('error');
      if (error) {
        return this.presentToast(error);
      }
    }
  }

  goToStatsPage(channelId: string): Promise<any> {
    Cookies.set('id', channelId, { expires: 30 });
    return this.navCtrl.push(StatsPage, { 'id': channelId });
  }

  presentToast(error: string) {
    const toast = this.toastCtrl
      .create({
                message: error,
                duration: 10000,
                position: 'top',
              });
    return toast.present();
  }

  presentEnterChannelIdAlert() {
    const alert = this.alertCtrl
      .create({
                title: 'Enter Channel Id',
                inputs: [
                  {
                    name: 'channelId',
                    placeholder: 'Channel Id',
                  },
                ],
                buttons: [
                  {
                    text: 'Cancel',
                    role: 'cancel',
                  },
                  {
                    text: 'Submit',
                    handler: data => {
                      this.channelService.updateCahnnelId(data.channelId);
                      this.goToStatsPage(data.channelId)
                        .catch(reason => this.logger.error(reason));
                    },
                  },
                ],
              });
    return alert.present();
  }

  presentQRCodeScannerModal() {
    const qrCodeScannerModal = this.modalCtrl.create(QRScannerPage);
    qrCodeScannerModal.present().catch(reason => this.logger.error(reason));
  }
}
