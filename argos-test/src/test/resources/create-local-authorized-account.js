function fn(input) {
    var permissions = input.permissions;
    var accountResponse = karate.call('classpath:feature/account/create-personal-account.feature', {
        name: 'Extra Person',
        email: 'local.permissions@extra.go'
    });
    if (accountResponse.responseStatus === 200) {
        var labelResponse = karate.call('classpath:feature/label/create-label.feature', {name: 'otherlabel'});
        if (labelResponse.responseStatus === 201) {
            var permissionsResponse = karate.call('classpath:feature/account/set-local-permissions.feature', {
                accountId: accountResponse.response.id,
                labelId: labelResponse.response.id,
                permissions: permissions
            });
            if (permissionsResponse.responseStatus === 200) {
                return {
                    accountId: accountResponse.response.id,
                    labelId: labelResponse.response.id,
                    token: accountResponse.response.token
                }
            }
        }
    }
}