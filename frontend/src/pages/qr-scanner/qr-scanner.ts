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

import { Component, ViewChild } from '@angular/core';
import { ZXingScannerComponent } from '@zxing/ngx-scanner';
import { NavController, ToastController, ViewController } from 'ionic-angular';
import { Logger, LoggingService } from 'ionic-logging-service';

import { StatsPage } from '../stats/stats';

@Component({
             selector: 'page-qr-scanner',
             templateUrl: 'qr-scanner.html',
           })
export class QRScannerPage {

  @ViewChild('scanner')
  private scanner: ZXingScannerComponent;

  private readonly uuidRegex: RegExp;
  private readonly logger: Logger;

  private availableCameras: MediaDeviceInfo[];
  private activeCamera: MediaDeviceInfo;

  constructor(private readonly navCtrl: NavController,
              private readonly toastCtrl: ToastController,
              private readonly viewCtrl: ViewController,
              private readonly loggingService: LoggingService) {
    this.logger = this.loggingService.getLogger('QRScannerPage');
    this.uuidRegex =
      new RegExp('[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}');
  }

  chooseCamera(cameras: MediaDeviceInfo[]) {
    if (cameras.length === 0) {
      return this.close().then(() => this.presentToast('No cameras available!'));
    }

    this.availableCameras = cameras;

    const backCamera = cameras.find(camera => camera.label.indexOf('back') !== -1);
    const activeCamera = backCamera ? backCamera : cameras[0];
    this.setCamera(activeCamera);
  }

  setCamera(camera: MediaDeviceInfo) {
    this.logger.debug('setCamera', 'Setting active camera to ', camera);

    this.activeCamera = camera;
    this.scanner.changeDeviceById(camera.deviceId);
  }

  toggleCamera() {
    const newCamera = this.availableCameras.find(
      camera => camera.deviceId !== this.activeCamera.deviceId);
    this.setCamera(newCamera);
  }

  scanSuccess(scanResult: string) {
    if (!this.uuidRegex.test(scanResult)) {
      this.logger.warn('scanSuccess', 'invalid qr-code: ', scanResult);
      return this.presentToast('Invalid QR-Code');
    }

    return this.close()
      .then(() => this.navCtrl.push(StatsPage, { 'id': scanResult }));
  }

  scanFailure(event) {
    this.logger.warn('scanFailure', 'Failed to scan:', event);
    return this.presentToast('Failed to scan QR-Code');
  }

  presentToast(message: string) {
    const toast = this.toastCtrl
      .create({
                message,
                duration: 5000,
                position: 'top',
              });
    return toast.present();
  }

  close() {
    return this.viewCtrl.dismiss();
  }
}
