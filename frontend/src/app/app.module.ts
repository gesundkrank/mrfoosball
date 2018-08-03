import {ErrorHandler, NgModule} from "@angular/core";
import {IonicApp, IonicErrorHandler, IonicModule} from "ionic-angular";

import {MyApp} from "./app.component";
import {MatchPage} from "../pages/match/match";
import {TournamentController} from "../controllers/tournamentController";
import {StatsPage} from "../pages/stats/stats";
import {IndexPage} from "../pages/index";
import {ZXingScannerModule} from "@zxing/ngx-scanner";
import {QRScannerPage} from "../pages/qr-scanner/qr-scanner";
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from "@angular/common/http";

@NgModule({
  declarations: [
    MyApp,
    IndexPage,
    MatchPage,
    StatsPage,
    QRScannerPage,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    IonicModule.forRoot(MyApp),
    ZXingScannerModule,
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    IndexPage,
    MatchPage,
    StatsPage,
    QRScannerPage,
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    TournamentController,
  ],
})
export class AppModule {
}
