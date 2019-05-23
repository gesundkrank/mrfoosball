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
import { PlayerStatsPage } from '../pages/stats/playerStats/playerStats';
import { StatsPage } from '../pages/stats/stats';
import { TeamStatsPage } from '../pages/stats/teamStats/teamStats';

import { MyApp } from './app.component';
import { ChannelService } from './channel.service';

@NgModule({
            declarations: [
              MyApp,
              IndexPage,
              MatchPage,
              StatsPage,
              TeamStatsPage,
              PlayerStatsPage,
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
              TeamStatsPage,
              PlayerStatsPage,
              QRScannerPage,
            ],
            providers: [
              { provide: ErrorHandler, useClass: IonicErrorHandler },
              LoggingService,
              TournamentController,
              [ChannelService],
            ],
          })
export class AppModule {
}
