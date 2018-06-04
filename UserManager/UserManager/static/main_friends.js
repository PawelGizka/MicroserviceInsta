$().ready(function() {
    display()
});

function follow(id, follow) {
    $.post('/ajax/follow/', {
        'id': id
    }, function (res) {
        console.log('user followed');
        console.log(res);

        display();

    }).fail(function (res) {
        console.log('following failed');
        console.log(res)
    });
}

function display() {
    $.get('ajax/get_friends/', function(res) {
        console.log(res)

        $("#content ul").empty();

        for (var i in res.users) {
            var user = res.users[i];
            var username = user.username;
            console.log(user.follows);
            if (user.follows) {
                $("#content ul").append(`<li>${user.username} <button onclick="follow(${user.id})">Stop following</button></li>`);
            } else {
                $("#content ul").append(`<li>${user.username} <button onclick="follow(${user.id})">Follow</button></li>`);
            }
        }

    }).fail(function() {
        console.log('retrieving friends failed');
    });
}