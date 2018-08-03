import {ErrorHandler, NgModule} from "@angular/core";
import {IonicApp, IonicErrorHandler, IonicModule} from "ionic-angular";

import {MyApp} from "./app.component";
import {MatchPage} from "../pages/match/match";
import {TournamentController} from "../controllers/tournamentController";
import {StatsPage} from "../pages/stats/stats";
import {IndexPage} from "../pages/index";
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from "@angular/common/http";

@NgModule({
  declarations: [
    MyApp,
    IndexPage,
    MatchPage,
    StatsPage,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    IndexPage,
    MatchPage,
    StatsPage,
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    TournamentController,
  ],
})
export class AppModule {
}
