import { NgModule } from '@angular/core';
import { ErrorHandler } from '@angular/core';
import { IonicApp } from 'ionic-angular';
import { IonicModule } from 'ionic-angular';
import { IonicErrorHandler } from 'ionic-angular';

import { MyApp } from './app.component';
import { MatchPage } from '../pages/match/match';
import { TournamentController } from '../controllers/tournamentController';
import { StatsPage } from '../pages/stats/stats';


@NgModule({
  declarations: [
    MyApp,
    MatchPage,
    StatsPage,
  ],
  imports: [
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    MatchPage,
    StatsPage,
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    TournamentController,
  ],
})
export class AppModule {}
