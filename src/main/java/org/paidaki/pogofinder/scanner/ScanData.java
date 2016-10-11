package org.paidaki.pogofinder.scanner;

import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScanData {

    private PokeScan pokeScan;
    private ArrayList<CatchablePokemon> pokemon;
    private ArrayList<Gym> gyms;
    private ArrayList<Pokestop> pokestops;

    public ScanData() {
        pokeScan = null;
        pokemon = new ArrayList<>();
        gyms = new ArrayList<>();
        pokestops = new ArrayList<>();
    }

    public PokeScan getPokeScan() {
        return pokeScan;
    }

    public void setPokeScan(PokeScan pokeScan) {
        this.pokeScan = pokeScan;
    }

    public ArrayList<CatchablePokemon> getPokemon() {
        return pokemon;
    }

    public void addPokemon(List<CatchablePokemon> pokemon) {
        this.pokemon.addAll(pokemon);
    }

    public ArrayList<Gym> getGyms() {
        return gyms;
    }

    public void addGyms(List<Gym> gyms) {
        this.gyms.addAll(gyms);
    }

    public ArrayList<Pokestop> getPokestops() {
        return pokestops;
    }

    public void addPokestops(Collection<Pokestop> pokestops) {
        this.pokestops.addAll(pokestops);
    }
}
