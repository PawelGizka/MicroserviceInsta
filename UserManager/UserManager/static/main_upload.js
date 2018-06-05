var file;

function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            $('#blah')
                .attr('src', e.target.result);
        };

        file = input.files[0];

        reader.readAsDataURL(input.files[0]);
    }
}

function uploadPhoto() {
    console.log("upload photo called");
    var token = $('#authentication-token').html();
    console.log($('#file-input')[0].files[0]);

    var formData = new FormData();
    formData.append('photo', file);
    // formData.append('photo', "photoData");

    $.ajax({
        type: "POST",
        beforeSend: function(request) {
            request.setRequestHeader("Authentication", token);
        },
        cache: false,
        url: "http://localhost:8080/photos",
        data: formData,
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function () {
            alert("Data Uploaded: ");
            location.href="main_photos"
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Handle errors here
            console.log('ERRORS: ' + textStatus);
            console.log(jqXHR)
            console.log(errorThrown)
            location.href="main_photos"
        }
    });
}