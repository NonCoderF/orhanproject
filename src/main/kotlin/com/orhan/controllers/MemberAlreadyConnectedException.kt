package com.orhan.controllers

class MemberAlreadyConnectedException: Exception(
    "You are already connected. I am not serving you!"
)