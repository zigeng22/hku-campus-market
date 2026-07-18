package com.example.a7506_project.model;

public final class User {
    private final long id;
    private final String nickname;
    private final String whatsapp;
    private final long createdAt;

    public User(long id, String nickname, String whatsapp, long createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.whatsapp = whatsapp;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getWhatsapp() { return whatsapp; }
    public long getCreatedAt() { return createdAt; }
}
