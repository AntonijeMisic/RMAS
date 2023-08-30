package com.example.rmasprojekat.Models

import java.io.Serializable

class Korisnik(
    public var id: String,
    public var ime: String,
    public var prezime: String,
    public var username: String,
    public var email: String,
    public var password: String,
    public var slika: String,
    public var poeni: Int
    //treba da se doda za sliku
){
    constructor(): this("", "", "", "", "", "", "", 0)
}