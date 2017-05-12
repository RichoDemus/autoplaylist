$(function ()
{
    $('#adminModal').on('shown.bs.modal', function (e)
    {
        Api.getDownloadJobStatus(status =>
        {
            if(status.running) {
                $("#downloadJobStatus").text("Download job status: Running...");
            }
            else
            {
                $("#downloadJobStatus").text("Download job status: Not Running...");
                $("#runDownloadJobButton").prop("disabled",false);
            }

            $("#downloadJobLastRun").text("Download job last run: " + status.lastRun);

        });
    });

    Service.init();
});
