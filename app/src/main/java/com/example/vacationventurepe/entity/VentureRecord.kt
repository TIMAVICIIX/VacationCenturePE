package com.example.vacationventurepe.entity

class VentureRecord(
    val studentCode:String,
    val ventureCode:String,
    val ventureName:String,
    val studentName:String,
    var destination:String,
    var ventureDes:String
){
    constructor() : this("", "", "", "", "", "")
}