import { Component } from '@angular/core';
import { AlertController, ModalController, NavController, ToastController } from 'ionic-angular';
import { Logger, LoggingService } from 'ionic-logging-service';

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
              private readonly loggingService: LoggingService) {
    this.logger = this.loggingService.getLogger('IndexPage');
  }

  ionViewDidEnter() {
    const splits = location.search.substring(1).split('=');
    if (splits && splits.length === 2 && splits[0] === 'id') {
      return this.goToStatsPage(splits[1]);
    } else {
      window.history.pushState(undefined, undefined, '/');

      const error = this.navCtrl.getActive().getNavParams().get('error');
      if (error) {
        return this.presentToast(error);
      }
    }
  }

  goToStatsPage(channelId: string): Promise<any> {
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
