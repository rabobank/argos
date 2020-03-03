function fn(auth) {
    var token = auth.token;
    var username = auth.username;
    var password = auth.password;

    if (token) {
        return {
            Authorization: 'Bearer ' + token,
            'Content-Type': 'application/json'
        };
    } else if (username && password) {
        var temp = username + ':' + password;
        var Base64 = Java.type('java.util.Base64');
        var encoded = Base64.getEncoder().encodeToString(temp.bytes);
        return {
            Authorization: 'Basic ' + encoded,
            'Content-Type': 'application/json'
        };
    } else {
        return {};
    }
}