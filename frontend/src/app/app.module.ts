import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';

import { MyApp } from './app.component';
import { MatchPage } from '../pages/match/match';
import { Tournament } from '../providers/tournament';
import { StartPage } from '../pages/start/start';
import { StatsPage } from '../pages/stats/stats';


@NgModule({
  declarations: [
    MyApp,
    MatchPage,
    StartPage,
    StatsPage,
  ],
  imports: [
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    MatchPage,
    StartPage,
    StatsPage,
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    Tournament,
  ],
})
export class AppModule {}
