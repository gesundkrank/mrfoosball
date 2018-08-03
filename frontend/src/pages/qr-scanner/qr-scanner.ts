import {Component, ViewChild} from "@angular/core";
import {NavController, ToastController, ViewController} from "ionic-angular";

import {ZXingScannerComponent} from "@zxing/ngx-scanner";
import {StatsPage} from "../stats/stats";

@Component({
             selector: 'page-qr-scanner',
             templateUrl: 'qr-scanner.html'
           })
export class QRScannerPage {

  @ViewChild('scanner')
  private scanner: ZXingScannerComponent;

  private readonly uuidRegex: RegExp;

  private availableCameras: MediaDeviceInfo[];
  private activeCamera: MediaDeviceInfo;

  constructor(private readonly navCtrl: NavController,
              private readonly toastCtrl: ToastController,
              private readonly viewCtrl: ViewController) {
    this.uuidRegex =
      new RegExp("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}");
  }

  ionViewDidEnter() {
  }

  chooseCamera(cameras: MediaDeviceInfo[]) {
    console.log(cameras);

    if (cameras.length == 0) {
      this.presentToast("No cameras available!");
      this.close();
      return;
    }

    this.availableCameras = cameras;

    let camera;
    const backCamera = cameras.find(camera => camera.label.indexOf("back") != -1);
    if (backCamera) {
      camera = backCamera;
    } else {
      camera = cameras[0]
    }
    this.setCamera(camera)
  }

  setCamera(camera: MediaDeviceInfo) {
    console.log("Setting active camera to ", camera);

    this.activeCamera = camera;
    this.scanner.changeDeviceById(camera.deviceId);
  }

  toggleCamera() {
    const newCamera = this.availableCameras.find(
      camera => camera.deviceId != this.activeCamera.deviceId);
    this.setCamera(newCamera)
  }

  scanSuccess(scanResult: string) {
    if (!this.uuidRegex.test(scanResult)) {
      console.log("invalid qr-code: " + scanResult);
      this.presentToast("Invalid QR-Code");
      return;
    }

    this.close();
    this.navCtrl.push(StatsPage, {'id': scanResult})
  }

  scanFailure(event) {
    console.log(event);
    this.presentToast("Failed to scan QR-Code");
  }

  presentToast(message: string) {
    const toast = this.toastCtrl
      .create({
                message: message,
                duration: 5000,
                position: 'top'
              });
    toast.present();
  }

  close() {
    this.viewCtrl.dismiss();
  }
}
