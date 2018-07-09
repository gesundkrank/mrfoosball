import {Component} from "@angular/core";
import {NavController} from "ionic-angular";
import {Http} from "@angular/http";
import {MatchPage} from "../match/match";
import {TournamentController} from "../../controllers/tournamentController";
import {Player, Tournament} from "../../models/tournament";
import {PlayerSkill} from "../../models/playerSkill";

const QUEUE_URL = '/api/tournament/queue';
const STAT_URL = '/api/stats';
const TOURNAMENTS_URL = '/api/tournament?num=10';

@Component({
  selector: 'page-stats',
  templateUrl: 'stats.html'
})
export class StatsPage {

  queue: Array<Player>;
  stats: Array<PlayerSkill>;
  lastTournaments: Array<Tournament>;

  constructor(
    public http: Http,
    private readonly navCtrl: NavController,
    private readonly tournamentController: TournamentController,
  ) {
    //
  }

  ionViewDidEnter() {
    this.checkTournament();
    this.loadPlayerStats();
    this.loadLastTournaments();
  }

  refresh(refresher) {
    this.checkTournament(false)
      .then(() => refresher.complete());
  }

  checkTournament(loop = true) {
    return this.tournamentController.getRunningMatch()
      .then((match) => {
        if (match) {
          this.newMatch();
        } else {
          this.loadQueue();
          if (loop) {
            setTimeout(() => {
              this.checkTournament();
            }, 5000);
          }
        }
      })
  }

  loadQueue() {
    this.http.get(QUEUE_URL)
      .map(res => res.json())
      .toPromise()
      .then(queue => {
        this.queue = queue;
      });
  }

  loadPlayerStats() {
    this.http.get(STAT_URL)
      .map(res => res.json())
      .toPromise()
      .then(stats => {
        this.stats = stats;
      });
  }

  loadLastTournaments() {
    this.http.get(TOURNAMENTS_URL)
      .map(res => res.json())
      .toPromise()
      .then(lastTournaments => {
        this.lastTournaments = lastTournaments;
      });
  }

  newMatch() {
    this.navCtrl.push(MatchPage);
  }

  roundSkill(skill: number) {
    return skill ? skill.toFixed(2): '';
  }

  private wins(tournament: Tournament): { [key: string]: number } {
    console.log(tournament);
    return this.tournamentController.countWins(tournament.matches)
  }

  winsTeamA(tournament: Tournament): number {
    return this.wins(tournament)['teamA'];
  }

  winsTeamB(tournament: Tournament): number {
    return this.wins(tournament)['teamB'];
  }

  winRate(playerSkill: PlayerSkill): string {
    return ((playerSkill.wins / playerSkill.games) * 100).toFixed(2) + " %";
  }

}
