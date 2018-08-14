import { HttpClientModule } from '@angular/common/http';
import { ErrorHandler, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ZXingScannerModule } from '@zxing/ngx-scanner';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { LoggingService } from 'ionic-logging-service';

import { TournamentController } from '../controllers/tournamentController';
import { IndexPage } from '../pages/index';
import { MatchPage } from '../pages/match/match';
import { QRScannerPage } from '../pages/qr-scanner/qr-scanner';
import { StatsPage } from '../pages/stats/stats';

import { MyApp } from './app.component';

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
              { provide: ErrorHandler, useClass: IonicErrorHandler },
              LoggingService,
              TournamentController,
            ],
          })
export class AppModule {
}
