const MAPBOX_TILES_URL = "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw";
const OSM_TILES_URL = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
const HOME_LOC = new L.LatLng(40.609436, 22.950101);
const RADIUS_METERS = 70;
const POKE_ICON_HEIGHT = 25;
const DESPAWN_TIMER_WIDTH = 35;
const MARKER_OFFSET_STEP = 0.0000001;
const INITIAL_SPEED = 50.5;
const MIN_SLIDER_VALUE = 1;
const MAX_SLIDER_VALUE = 100;
const SPEED_BOOSTER = 30;
const DEFAULT_SCAN_CIRCLE_EXPIRE_MS = 1000;
var POKEMON_DATA = loadJSONData();

var map;
var marker;
var markerCircle;
var pokemonMarkers;
var scanCircles;
var follow;
var showScanCircles;
var showForts;
var scanOnly;
var speedBoost;
var speed;
var slider;
var tooltipTimeout;
var gyms;
var gymIcons;
var pokestops;
var pokestopIcon;
var scanAreas;

L.PokemonIcon = L.Icon.extend({

    createIcon: function () {
        var containerDiv = document.createElement("div");
        var img = new Image();
        var iconObj = this;

        img.onload = function () {
            var adj = img.height / POKE_ICON_HEIGHT;
            var adjWidth = img.width / adj;
            adjWidth = adjWidth < DESPAWN_TIMER_WIDTH ? DESPAWN_TIMER_WIDTH : adjWidth;

            $(containerDiv).css({left: "-" + adjWidth / 2 + "px"});
            $(containerDiv).css({top: "-" + POKE_ICON_HEIGHT + "px"});

            L.Util.setOptions(iconObj, {
                popupAnchor: [0, -POKE_ICON_HEIGHT + 3]
            });
            $(containerDiv).show();
        };
        img.src = "assets/images/sprites/" + this.options.pokemon.id + ".png";

        $(containerDiv).hide();
        containerDiv.className = "containerDiv";
        containerDiv.innerHTML =
            "<div class='pokeImage'>" +
            "<img style='max-height: " + POKE_ICON_HEIGHT + "px' alt='" + this.options.pokemon.name +
            "' src='" + img.src + "'>" +
            "</div>" +
            "<div style='min-width: " + DESPAWN_TIMER_WIDTH + "px' class='despawnTimer' data-expire='" + this.options.pokemon.despawnTime +
            "' data-bugged='" + this.options.pokemon.isBugged + "'>" +
            getRemainingTimer(this.options.pokemon.despawnTime, this.options.pokemon.isBugged) +
            "</div>";

        return containerDiv;
    }
});

function initialize() {
    String.prototype.capitalize = function () {
        return this.charAt(0).toUpperCase() + this.slice(1).toLowerCase();
    }
}

function initMap() {
    var PokeballIcon = L.Icon.Default.extend({
        options: {
            iconUrl: "assets/images/app/pokeball-marker.png"
        }
    });
    marker = L.marker(HOME_LOC, {
        icon: new PokeballIcon(),
        draggable: true
    });
    markerCircle = L.circle(HOME_LOC, {
        radius: RADIUS_METERS,
        interactive: false
    });
    gyms = new L.FeatureGroup();
    gymIcons = {
        unrecognized: L.icon({
            iconUrl: "assets/images/app/gym-marker-unrecognized.png",
            iconSize: [33, 37],
            iconAnchor: [14, 36]
        }),
        neutral: L.icon({
            iconUrl: "assets/images/app/gym-marker-neutral.png",
            iconSize: [33, 37],
            iconAnchor: [14, 36]
        }),
        blue: L.icon({
            iconUrl: "assets/images/app/gym-marker-blue.png",
            iconSize: [33, 37],
            iconAnchor: [14, 36]
        }),
        red: L.icon({
            iconUrl: "assets/images/app/gym-marker-red.png",
            iconSize: [33, 37],
            iconAnchor: [14, 36]
        }),
        yellow: L.icon({
            iconUrl: "assets/images/app/gym-marker-yellow.png",
            iconSize: [33, 37],
            iconAnchor: [14, 36]
        })
    };
    pokestops = new L.FeatureGroup();
    pokestopIcon = {
        noLure: L.icon({
            iconUrl: "assets/images/app/pokestop-marker.png",
            iconSize: [33, 57],
            iconAnchor: [15, 55]
        }),
        lured: L.icon({
            iconUrl: "assets/images/app/pokestop-lured-marker.png",
            iconSize: [33, 57],
            iconAnchor: [15, 55]
        })
    };
    scanAreas = new L.FeatureGroup();
    var mapBoxTiles = L.tileLayer(MAPBOX_TILES_URL, {
        maxZoom: 19,
        attribution: "Map data &copy; <a href='http://openstreetmap.org'>OpenStreetMap</a>",
        id: "mapbox.streets"
    });
    var osmTiles = L.tileLayer(OSM_TILES_URL, {
        maxZoom: 19,
        attribution: "Map data &copy; <a href='http://openstreetmap.org'>OpenStreetMap</a>"
    });
    var baseLayers = {
        OpenStreetMap: osmTiles,
        MapBox: mapBoxTiles
    };
    var legend = createLegend();
    var filter = createPokemonFilter();
    var spinner = createSpinner();
    slider = createSlider();
    speed = slider.noUiSlider.get();
    initializeToggleButtons();

    follow = true;
    scanOnly = true;
    speedBoost = false;
    showScanCircles = true;
    showForts = 0;
    scanCircles = [];
    pokemonMarkers = [];
    map = L.map("mapDiv", {
        center: HOME_LOC,
        zoom: 17
    });

    map.doubleClickZoom.disable();
    map.addLayer(mapBoxTiles);
    map.addLayer(marker);
    map.addLayer(scanAreas);
    map.addControl(L.control.layers(baseLayers));
    map.addControl(legend);
    map.addControl(filter);
    map.addControl(spinner);

    initializeListeners();
}

function initializeListeners() {
    map.on("click", function (event) {
        var loc = event.latlng;

        if (!scanOnly) moveMarker(loc);
        scanForPokemon(loc);
    });
    map.on("contextmenu", function (event) {
        var loc = event.latlng;

        moveMarker(loc);
    });
    marker.bindPopup(function () {
        return "<b>Location:</b><br>" + marker.getLatLng().lat.toFixed(6) + ", " + marker.getLatLng().lng.toFixed(6);
    }, {
        className: "custom-popup"
    });
    marker.on("drag", function (event) {
        var loc = event.latlng;

        markerCircle.setLatLng(loc);
    });
    marker.on("dragend", function (event) {
        var loc = event.target._latlng;

        map.panTo(loc);
        scanForPokemon(loc);
    });
    $("#pin-mode").change(function () {
        $(".filter-Container").toggleClass("filter-Pin", this.checked);
    });
    $("#select-all").change(function () {
        var status = this.checked;

        $(".filter-Checkbox").each(function () {
            this.checked = status;
            $(this).trigger("change");
        });
    });
    $(".filter-Content-Options span").click(function () {
        var cb = $(this).children(":checkbox");

        cb.prop("checked", !cb.prop("checked"));
        cb.trigger("change");
    });
    $(".filter-Content-Options span").find(":checkbox").click(function (event) {
        event.stopPropagation();
    });
}

function initializeToggleButtons() {
    $("#X").tooltip({
        title: "Toggle Scan Only",
        trigger: "focus hover",
        placement: "bottom"
    });
    $("#Y").tooltip({
        title: "Toggle Follow Player",
        trigger: "focus hover",
        placement: "bottom"
    });
    $("#B").tooltip({
        title: "Show / Hide Scan Circles",
        trigger: "focus hover",
        placement: "bottom"
    });
    $("#A").tooltip({
        title: "Scan For Pokemon",
        trigger: "focus hover",
        placement: "bottom"
    });
    $("#Start").tooltip({
        title: "Show / Hide Forts",
        trigger: "focus hover",
        placement: "bottom"
    });
}

function createLegend() {
    L.Control.Legend = L.Control.extend({
        options: {
            position: "bottomleft"
        },

        onAdd: function () {
            var controlDiv = L.DomUtil.create("div", "legend-Container");
            L.DomEvent
                .addListener(controlDiv, "contextmenu", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "contextmenu", L.DomEvent.preventDefault)
                .addListener(controlDiv, "mousedown", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "mousedown", L.DomEvent.preventDefault)
                .addListener(controlDiv, "click", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "click", function () {
                    reconnectController();
                });

            var controlUI = L.DomUtil.create("div", "legend-Interior", controlDiv);
            controlUI.title = "Scan for Logitech F310";

            return controlDiv;
        }
    });

    L.control.legend = function (options) {
        return new L.Control.Legend(options);
    };
    return L.control.legend();
}

function createPokemonMarker(pokemon) {
    var loc = new L.LatLng(pokemon.lat, pokemon.lng);

    var pokemonMarker = L.marker(loc, {
        icon: new L.PokemonIcon({pokemon: pokemon})
    });
    pokemonMarker.bindPopup(function () {
        return "<div style='text-align: center'><img alt='" + pokemon.name +
            "' src='assets/images/sprites/" + pokemon.id + ".png'>" +
            "<br><strong><i>" + pokemon.name + "</i></strong>" +
            "<br><br>Location<br>" + pokemon.lat.toFixed(6) + ", " + pokemon.lng.toFixed(6) +
            "<br><br>Despawns in<br><div class='despawnTimerPopup' data-expire='" +
            pokemon.despawnTime + "' data-bugged='false'>" +
            getRemainingTimer(pokemon.despawnTime, false) +
            "</div></div>";
    }, {
        maxWidth: 500,
        className: "custom-popup"
    });
    return pokemonMarker;
}

function createPokemonFilter() {
    L.Control.Filter = L.Control.extend({
        options: {
            position: "topleft"
        },

        onAdd: function () {
            var controlDiv = L.DomUtil.create("div", "filter-Container");
            L.DomEvent
                .addListener(controlDiv, "click", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "wheel", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "contextmenu", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "contextmenu", L.DomEvent.preventDefault)
                .addListener(controlDiv, "mousedown", L.DomEvent.stopPropagation)
                .addListener(controlDiv, "mousedown", L.DomEvent.preventDefault);

            var controlUI = L.DomUtil.create("div", "filter-Interior", controlDiv);
            controlUI.innerHTML =
                "<div class='filter-Title'>" +
                "  <img class='center left' src='https://fastpokemap.se/img/favicon-32x32.png'>" +
                "  <img class='center right' src='https://fastpokemap.se/img/favicon-32x32.png'>" +
                "  <span class='center'> Pokémon Filter </span>" +
                "</div>" +
                "<div class='filter-Content'>" +
                "  <div class='filter-Content-Options'>" +
                "    <span><label>Pin</label><input id='pin-mode' type='checkbox'></span>" +
                "    <span><label>Select All</label><input id='select-all' type='checkbox'></span>" +
                "  </div>" +
                "  <ul class='filter-List'>" +
                "  </ul>" +
                "</div>";

            return controlDiv;
        }
    });

    L.control.filter = function (options) {
        return new L.Control.Filter(options);
    };
    return L.control.filter();
}

function createSpinner() {
    return L.Control.loading({
        position: "bottomright",
        separate: true,
        spinjs: true,
        spin: {
            lines: 9,
            length: 3,
            width: 3,
            radius: 5,
            rotate: 13
        }
    })
}

function reconnectController() {
    java.reconnectController();
}

function createSlider() {
    var slider = document.getElementById("speedSlider");

    noUiSlider.create(slider, {
        start: INITIAL_SPEED,
        connect: "lower",
        range: {
            min: MIN_SLIDER_VALUE,
            max: MAX_SLIDER_VALUE
        }
    });
    slider.noUiSlider.on("update", function () {
        speed = slider.noUiSlider.get();
        $(".tooltipText").html(speed);
    });

    return slider;
}

function moveMarker(loc) {
    marker.closePopup();
    marker.setLatLng(loc);
    markerCircle.setLatLng(loc);
    if (follow) map.panTo(loc);
}

function scanForPokemon(loc) {
    java.userPokemonScan(loc.lat, loc.lng);
}

function updateMarkers() {
    removeExpiredMarkers();

    $(".despawnTimer, .despawnTimerPopup").each(function () {
        updateRemainingTimer(this);
    });
}

function updateRemainingTimer(element) {
    var despawnTime = $(element).data("expire");
    var isBugged = $(element).data("bugged");
    $(element).html(getRemainingTimer(despawnTime, isBugged));
}

function getRemainingTimer(despawnTime, isBugged) {
    if (isBugged) {
        console.log("asdasd");
        return "??:??";
    }
    var timer = "";
    var timeRemainingMs = despawnTime - Date.now();
    timeRemainingMs = timeRemainingMs < 0 ? 0 : timeRemainingMs;

    var seconds = Math.floor(( timeRemainingMs / 1000 ) % 60);
    var minutes = Math.floor(( ( timeRemainingMs / ( 1000 * 60 ) ) % 60 ));
    var hours = Math.floor(( ( timeRemainingMs / ( 1000 * 60 * 60 ) ) % 24 ));

    if (hours != 0) {
        if (hours < 10) {
            timer += "0";
        }
        timer += hours + ":";
    }
    if (minutes < 10) {
        timer += "0";
    }
    timer += minutes + ":";
    if (seconds < 10) {
        timer += "0";
    }
    timer += seconds;

    return timer;
}

function removeExpiredMarkers() {
    for (var i in pokemonMarkers) {
        var groupMarkers = pokemonMarkers[i].markers;

        for (var j in groupMarkers) {
            var despawnTime = groupMarkers[j].pokemon.despawnTime;
            var currentTime = Date.now();

            if (despawnTime <= currentTime) {
                pokemonMarkers[i].layer.removeLayer(groupMarkers[j].marker);

                if (Object.keys(groupMarkers) < 1) {
                    removeFilter(groupMarkers[j].pokemon.id);
                }
                delete groupMarkers[j];
            }
        }
    }
}

function calculateZoomOffset(zoom) {
    return Math.pow(Math.E, ((35171 - 5000 * zoom) / 7215));
}

function createFilter(pokemon) {
    var li = document.createElement("li");
    var img = L.DomUtil.create("img", "left");
    var label = document.createElement("label");
    var checkbox = L.DomUtil.create("input", "filter-Checkbox");

    $(li).addClass("rarity-" + pokemon.rarity);
    $(li).attr("data-rarity", pokemon.rarity);
    $(li).attr("id", "filter-" + pokemon.id);
    $(li).click(function () {
        var cb = $(this).children(":checkbox");

        cb.prop("checked", !cb.prop("checked"));
        cb.trigger("change");
    });

    $(img).css({
        "content": "url('assets/images/sprites/" + pokemon.id + ".png')",
        "max-width": "60px",
        "max-height": "32px"
    });

    label.innerHTML = pokemon.name.capitalize() + " - #" + pokemon.id;

    checkbox.type = "checkbox";
    checkbox.checked = true;
    $(checkbox).click(function (event) {
        event.stopPropagation();
    });
    $(checkbox).change(function () {
        if (!this.checked) {
            map.removeLayer(pokemonMarkers[pokemon.id].layer);
        } else {
            map.addLayer(pokemonMarkers[pokemon.id].layer);
        }
        $("#select-all")[0].checked = $(".filter-Checkbox:checked").length == $(".filter-Checkbox").length;
    });
    $(checkbox).trigger("change");

    $(li).append(img)
        .append(label)
        .append(checkbox);

    $(".filter-List").append(li);
    $(".filter-List li").sort(function (a, b) {
        return (parseInt($(a).attr("data-rarity")) < parseInt($(b).attr("data-rarity"))) ? 1 : -1;
    }).appendTo(".filter-List");
}

function addFilter(pokemonId) {
    $("#filter-" + pokemonId).removeClass("hide");
    $(".filter-List > li:visible:last").addClass("filter-Last");
}

function removeFilter(pokemonID) {
    var li = $("#filter-" + pokemonID);
    var cb = $(li).children(":checkbox");

    $(".filter-List > li:visible:last").removeClass("filter-Last");
    li.addClass("hide");
    $(".filter-List > li:visible:last").addClass("filter-Last");
    cb.prop("checked", true);
    cb.trigger("change");
}

function loadJSONData() {
    $.getJSON("assets/data/pokemon.json", function (data) {
        POKEMON_DATA = data;
    });
}
