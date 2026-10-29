package org.health.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private String model;
    private int remainingRequests;
    private String disclaimer;
    private List<String> workflow;
    private List<String> sources;
}
