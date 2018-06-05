$().ready(function() {

    display()
});


function display() {
    var token = $('#authentication-token').html();
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/photos",
        beforeSend: function(request) {
            request.setRequestHeader("Authentication", token);
        },
        success: function (res) {
            console.log('photos received');
            console.log(res);
            displayPhotos(res);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Handle errors here
            console.log('ERRORS: ' + textStatus);
            console.log(jqXHR)
            console.log(errorThrown)
            // STOP LOADING SPINNER
        }
    });

}

function displayPhotos(photos) {
    $("#content ul").empty();
    for (var i in photos) {
        var photo = photos[i];

        var uploadedTime = new Date(photo.date).toLocaleTimeString();
        var uploadedDate = new Date(photo.date).toLocaleDateString();

        $("#content ul").append(`
                <li>
                    <h2>${photo.owner.username} on ${uploadedTime} ${uploadedDate}</h2>
                    <img src="${photo.photoPath}">
                </li>
        `);
    }
}