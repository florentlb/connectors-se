package org.talend.components.onedrive.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.talend.components.onedrive.messages.Messages;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@GridLayout({ @GridLayout.Row({ "authenticationLogin" }), @GridLayout.Row({ "authenticationPassword" }) })
@Documentation("'Login' authentication settings")
public class AuthenticationLoginPasswordConfiguration implements Serializable, AuthenticationConfiguration {

    @Option
    @Documentation("Authentication login for 'Login' authentication")
    private String authenticationLogin = "";

    @Option
    @Credential
    @Documentation("Authentication password for 'Login' authentication")
    private String authenticationPassword = "";

    @Override
    public void validate(Messages i18n) {
        if (authenticationLogin.isEmpty()) {
            throw new RuntimeException(i18n.healthCheckLoginIsEmpty());
        }
    }
}
