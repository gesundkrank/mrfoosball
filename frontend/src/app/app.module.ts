import { NgModule } from '@angular/core';
import { ErrorHandler } from '@angular/core';
import { IonicApp } from 'ionic-angular';
import { IonicModule } from 'ionic-angular';
import { IonicErrorHandler } from 'ionic-angular';
import { MockBackend } from '@angular/http/testing';
import { Http } from '@angular/http';
import { BaseRequestOptions } from '@angular/http';

import { MyApp } from './app.component';
import { MatchPage } from '../pages/match/match';
import { Tournament } from '../providers/tournament';
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
    Tournament,
    BaseRequestOptions,
    MockBackend,
    {
      provide: Http,
      deps: [MockBackend, BaseRequestOptions],
      useFactory: (backend, options) => { return new Http(backend, options); }
    }
  ],
})
export class AppModule {}
