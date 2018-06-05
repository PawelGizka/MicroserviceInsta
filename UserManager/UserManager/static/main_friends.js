$().ready(function() {
    console.log("dziala")

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
        console.log('chat messages received');
        console.log(res)

        $("#content").empty();

        for (var i in res.users) {
            var user = res.users[i];
            var username = user.username;

            if (user.follows) {
                $("#content").append(`
                            <div class="row">
                                <div class="col-sm-6">
                                    <h2>${user.username}</h2>
                                </div>
                                
                                <div class="col-sm-6">
                                    <button class="btn btn-primary " style="font-size: 30px" onclick="follow(${user.id})">Stop following</button>
                                </div>
                            </div>
                `);
            } else {
                $("#content").append(`
                            <div class="row">
                                <div class="col-sm-6">
                                    <h2>${user.username}</h2>
                                </div>
                                
                                <div class="col-sm-6">
                                    <button class="btn btn-primary " style="font-size: 30px" onclick="follow(${user.id})">Follow</button>
                                </div>
                            </div>
                `);
            }
        }

    }).fail(function() {
        console.log('retrieving freinds messages failed');
    });
}