package pl.greywarden.tools.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EncryptionType {
    AES256CBC("AES-256-CBC");
    private final String name;
}
