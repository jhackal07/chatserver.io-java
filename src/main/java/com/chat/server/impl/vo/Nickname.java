package com.chat.server.impl.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Nickname {
    private String nickname;
    private String sessionId;
    private String oldNickname;
}
