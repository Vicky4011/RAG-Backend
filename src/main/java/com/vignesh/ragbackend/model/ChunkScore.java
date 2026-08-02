package com.vignesh.ragbackend.model;

public class ChunkScore {

    private final String chunk;
    private final double score;

    public ChunkScore(String chunk, double score) {
        this.chunk = chunk;
        this.score = score;
    }

    public String getChunk() {
        return chunk;
    }

    public double getScore() {
        return score;
    }
}