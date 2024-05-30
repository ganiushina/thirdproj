<!doctype html>
<html>
<head>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.0.0/jquery.min.js"></script>
</head>
<body>
<script>

        $(document).ready(function() {
              $.ajax({
                type: 'POST',
                url: '/examples/echo-message',
                data: {
                    message: 'hello'
                },
                success: function(text) {
                    $(document.body).text('Response: ' + text);
                },
                error: function (jqXHR) {
                    $(document.body).text('Error: ' + jqXHR.status);
                }
            });
        });

</script>
</body>
</html>