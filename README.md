# PoGoFinder

A Pokemon Go Scanner, that supports multiple accounts, scan on demand and auto-scanning using a SpawnPoints database

PoGoFinder is a multi-threaded desktop application, that utilizes the JavaFX's webengine to load the Leaflet map
There is communication between Java and Javascript

#### WARNING

:warning: Latest security update on Niantic's servers has crippled all Pokemon Go Scanners :warning:
:warning: WILL NOT work until further notice (Reversing in progress...) :warning:

## Features

* Companion app [XPoGoFinder](https://github.com/Nik-Bam/XPoGoFinder)
* Multiple accounts support ([accounts.json](data/accounts.json) file required)
* Support for Gamepad (currently only Logitech FX310) to control the player and the menu
* Runs a simple HTTP server, to serve [XPoGoFinder](https://github.com/Nik-Bam/XPoGoFinder) with coordinates and spoof the devices GPS
* Player movement speed adjustment
* Manual scan for pokemon
* Multiple parallel scans, until it runs out of accounts (then waits for available account)
* Automatic pokemon scanning (if provided with [data.json](data/data.json) file containing SpawnPoints)
* Live Pokemon list, with all the pokemon appearing on the map
* Pokemon filtering - choose which pokemon show on the map
* Pokemon details and time remaining until despawn
* Pokemon and Player coordinates
* Pokestops and Gyms show on map
* Automatic save if new data is found (Spawns, PokeStops, Gyms, Pokemon)

## Build With

* Java 8
* JavaFX
* CSS
* HTML
* Javascript
* JQuery, Bootstrap
* [Leaflet](https://github.com/Leaflet/Leaflet)
* [Leaflet Loading Control](https://github.com/ebrelsford/Leaflet.loading)
* [noUiSlider](https://refreshless.com/nouislider/)

## Examples

##### Accounts

Example [accounts.json](data/accounts.json) file

##### Spawns

Example [data.json](data/data.json) file

## Screenshots

![pogofinder-3](https://cloud.githubusercontent.com/assets/22759513/19285945/b75a5be8-9004-11e6-8b73-893528b90b5b.png)

![pogofinder-2](https://cloud.githubusercontent.com/assets/22759513/19285947/b7664480-9004-11e6-807b-03f350d215be.png)

![pogofinder-1](https://cloud.githubusercontent.com/assets/22759513/19285946/b75fba98-9004-11e6-93d7-ced560a823f5.png)

## Authors

* **Nikos Bampamis** - [Nik-Bam](https://github.com/Nik-Bam)

## License

This project is licensed under the GNU GPLv3 License - see the [LICENSE.md](LICENSE.md) file for details
