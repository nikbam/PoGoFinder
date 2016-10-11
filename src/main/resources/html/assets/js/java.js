function spawnPokemon(pokemon) {
    if (pokemonMarkers[pokemon.id] == undefined) {
        pokemonMarkers[pokemon.id] = {
            layer: new L.FeatureGroup(),
            markers: []
        };
        pokemon.rarity = POKEMON_DATA.pokemon[parseInt(pokemon.id) - 1].rarity;
        createFilter(pokemon);
    }
    var markers = pokemonMarkers[pokemon.id].markers;

    for (var i in markers) {
        var pokeSpawnId = markers[i].pokemon.spawnPointId;
        var encounterId = markers[i].pokemon.encounterId;
        var pokemonId = markers[i].pokemon.id;
        var pokemonExpire = markers[i].pokemon.despawnTime;

        if (pokemonId == pokemon.id && encounterId == pokemon.encounterId &&
            pokeSpawnId == pokemon.spawnPointId && pokemonExpire == pokemon.despawnTime) {
            return false;
        }
    }
    var pokemonMarker = createPokemonMarker(pokemon);

    markers.push({
        pokemon: pokemon,
        marker: pokemonMarker
    });
    pokemonMarkers[pokemon.id].layer.addLayer(pokemonMarker);
    pokemon.mapId = pokemonMarkers[pokemon.id].layer.getLayerId(pokemonMarker);

    if (Object.keys(markers).length < 1) {
        addFilter(pokemon.id);
    }
    return true;
}

function offsetMarker(modLat, modLng) {
    var boost = speedBoost ? SPEED_BOOSTER : 1;
    var loc = new L.LatLng(
        marker.getLatLng().lat + modLat * MARKER_OFFSET_STEP * speed * boost,
        marker.getLatLng().lng + modLng * MARKER_OFFSET_STEP * speed * boost);

    moveMarker(loc);
}

function offsetMap(modLat, modLng) {
    var zoomOffset = calculateZoomOffset(map.getZoom());
    var loc = new L.LatLng(
        map.getCenter().lat + modLat * zoomOffset,
        map.getCenter().lng + modLng * zoomOffset);
    follow = false;

    $("#Y").addClass("buttonDisabled");
    map.panTo(loc);
}

function toggleFollow() {
    $("#Y").toggleClass("buttonDisabled", follow);

    follow = !follow;
    if (follow) map.panTo(marker.getLatLng());
}

function toggleScanOnly() {
    $("#X").toggleClass("buttonDisabled", scanOnly);

    scanOnly = !scanOnly;
}

function toggleForts() {
    showForts = (showForts + 1) % 4;
    var start = $("#Start");

    switch (showForts) {
        case 1:
            start.removeClass("startDisabled");
            pokestops.remove();
            gyms.addTo(map);
            break;
        case 2:
            start.removeClass("startDisabled");
            gyms.remove();
            pokestops.addTo(map);
            break;
        case 3:
            start.removeClass("startDisabled");
            gyms.addTo(map);
            pokestops.addTo(map);
            break;
        default:
            start.addClass("startDisabled");
            gyms.remove();
            pokestops.remove();
            break;
    }
}

function toggleScanCircles() {
    $("#B").toggleClass("buttonDisabled", showScanCircles);

    showScanCircles = !showScanCircles;
    for (var i in scanCircles) {

        var sc = scanCircles[i];
        if (sc.expired == false) {
            if (showScanCircles) {
                sc.addTo(map);
            } else {
                sc.remove();
            }
        }
    }
}

function toggleSpeedBoost() {
    speedBoost = !speedBoost;

    $(".noUi-base").toggleClass("speedBoost", speedBoost);
}

function changeSpeed(modSpeed) {
    modSpeed = (modSpeed < 0) ? modSpeed - 1 : modSpeed + 1;
    var newValue = parseFloat(speed) + Math.round(modSpeed);
    var tooltip = $(".tooltipText");

    if (newValue < MIN_SLIDER_VALUE) {
        newValue = MIN_SLIDER_VALUE;
    } else if (newValue > MAX_SLIDER_VALUE) {
        newValue = MAX_SLIDER_VALUE;
    }
    if (newValue != speed) {
        speed = newValue;
        slider.noUiSlider.set(speed);
    }
    if (tooltip.css("visibility") == "hidden") {
        tooltip.css({
            visibility: "visible",
            opacity: "0.9"
        });
        tooltipTimeout = setTimeout(function () {
            tooltip.css({
                visibility: "hidden",
                opacity: "0"
            });
        }, 1000);
    } else {
        clearTimeout(tooltipTimeout);
        tooltipTimeout = setTimeout(function () {
            tooltip.css({
                visibility: "hidden",
                opacity: "0"
            });
        }, 1000);
    }
}

function addScanCircle(lat, lng) {
    var loc = new L.LatLng(lat, lng);
    var scanCircle = L.circle(loc, {
        radius: RADIUS_METERS,
        color: "#FF4433",
        interactive: false
    });
    scanCircle.expired = false;
    scanCircle.expireMs = DEFAULT_SCAN_CIRCLE_EXPIRE_MS;
    scanCircles.push(scanCircle);
    if (showScanCircles) map.addLayer(scanCircle);
    if (map.hasLayer(markerCircle)) markerCircle.bringToFront();

    return scanCircle;
}

function expireScanCircle(scanCircle) {
    setTimeout(function () {
        scanCircle.expired = true;
        scanCircle.remove();
    }, scanCircle.expireMs);
}

function scanStarted(removeMarkerCircle) {
    $("#A").toggleClass("buttonDisabled", removeMarkerCircle);

    loading(true);
    if (removeMarkerCircle) markerCircle.remove();
}

function loading(status) {
    if (status) {
        map.fire("dataloading");
    } else {
        map.fire("dataload");
    }
}

function doneScanning() {
    loading(false);
}

function readyToScan() {
    markerCircle.addTo(map);

    $("#A").removeClass("buttonDisabled");
}

function getCurrentLocation() {
    return marker.getLatLng();
}

function scanForPokemonOnMarker() {
    var loc = marker.getLatLng();

    scanForPokemon(loc);
}

function controllerConnected() {
    $(".legend-Interior").css({
        "background-color": "#27AE60",
        "box-shadow": "0 0 10px 5px #27AE60"
    });
}

function controllerDisconnected() {
    $(".legend-Interior").css({
        "background-color": "#C0392B",
        "box-shadow": "0 0 10px 5px #C0392B"
    });
}

function addScanArea(scanArea) {
    var mapId = scanArea.mapId;

    if (scanAreas.getLayer(mapId) != undefined) {
        return -1;
    }
    var area = L.rectangle([scanArea.northWest, scanArea.southEast], {
        color: "#FF4433",
        weight: 1,
        fill: false,
        interactive: false
    });
    scanAreas.addLayer(area);

    return scanAreas.getLayerId(area);
}

function addGymMarker(gym) {
    var mapId = gym.mapId;

    if (mapId != undefined) {
        var m = gyms.getLayer(mapId);

        m.setIcon(gymIcons[gym.team]);
        return -1;
    }
    var loc = new L.LatLng(gym.lat, gym.lng);
    var gymMarker = L.marker(loc, {
        icon: gymIcons[gym.team]
    });
    gyms.addLayer(gymMarker);

    return gyms.getLayerId(gymMarker);
}

function addPokestopMarker(pokestop) {
    var mapId = pokestop.mapId;

    if (mapId != undefined) {
        var m = pokestops.getLayer(mapId);
        var i = pokestop.hasLure ? pokestopIcon.lured : pokestopIcon.noLure;

        m.setIcon(i);
        return -1;
    }
    var loc = new L.LatLng(pokestop.lat, pokestop.lng);
    var icon = pokestop.hasLure ? pokestopIcon.lured : pokestopIcon.noLure;
    var pokestopMarker = L.marker(loc, {
        icon: icon
    });
    pokestops.addLayer(pokestopMarker);

    return pokestops.getLayerId(pokestopMarker);
}

function getScanOnly() {
    return scanOnly;
}
