package com.optcg.deckbuilder.model.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private Long id;
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime dateTime;
    private String location;
    private CreatorDTO creator;
    private Set<AttendeeDTO> attendees;
    private Integer attendeeCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatorDTO {
        private Long id;
        private String username;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendeeDTO {
        private Long id;
        private String username;
        private String avatarUrl;
    }
}
