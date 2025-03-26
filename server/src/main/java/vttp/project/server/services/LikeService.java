package vttp.project.server.services;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import vttp.project.server.repositories.LikeRepository;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepo;

    @Autowired
    private MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Integer> likeCounts = new ConcurrentHashMap<>();

    // public LikeMetricsService(MeterRegistry meterRegistry) {
    //     this.meterRegistry = meterRegistry;
    // }

    public void incrementLikes(String postId) {
        likeRepo.incrementLikes(postId);
        likeCounts.merge(postId, 1, Integer::sum);
        meterRegistry.counter("post_likes_total", Tags.of("postId", postId.toString()))
                    .increment();
    }

    public void decrementLikes(String postId) {
        likeRepo.decrementLikes(postId);
        likeCounts.merge(postId, -1, Integer::sum);
        meterRegistry.counter("post_likes_total", Tags.of("postId", postId.toString()))
                    .increment(-1);
    }

    public int getLikeCount(String postId) {
        int promLikes = likeCounts.getOrDefault(postId, 0);
        int mgLikes = likeRepo.getLikesCount(postId);
        return promLikes == likeRepo.getLikesCount(postId) ? promLikes : mgLikes;
    }
}

