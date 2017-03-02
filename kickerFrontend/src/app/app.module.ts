import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';

import { MyApp } from './app.component';
import { StartPage } from '../pages/start/start';


@NgModule({
  declarations: [
    MyApp,
    StartPage,
  ],
  imports: [
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    StartPage,
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
  ],
})
export class AppModule {}
