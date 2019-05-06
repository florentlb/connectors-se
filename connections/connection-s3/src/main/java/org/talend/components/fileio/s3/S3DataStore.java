package org.talend.components.fileio.s3;

import lombok.Getter;
import lombok.Setter;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

import static org.talend.sdk.component.api.component.Icon.IconType.FILE_S3_O;

@Getter
@Setter
@Icon(FILE_S3_O)
@Checkable("S3")
@DataStore("S3DataStore")
@Documentation("Datastore of a S3 source.")
@OptionsOrder({ "specifyCredentials", "accessKey", "secretKey" })
public class S3DataStore implements Serializable {

    @Option
    @Documentation("Should this datastore be secured and use access/secret keys.")
    private boolean specifyCredentials = true;

    @Option
    // @Required // todo: ensure ui supports to bypass this validation if not visible
    @ActiveIf(target = "specifyCredentials", value = "true")
    @Documentation("The S3 access key")
    private String accessKey;

    @Option
    @Credential
    // @Required // todo: ensure ui supports to bypass this validation if not visible
    @ActiveIf(target = "specifyCredentials", value = "true")
    @Documentation("The S3 secret key")
    private String secretKey;
}
