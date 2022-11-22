using aspnet6.Services;
using Microsoft.AspNetCore.Mvc;

namespace aspnet6.Controllers;

[ApiController]
[Route("[controller]")]
public class BlockingIoController : ControllerBase
{

    private readonly ILogger<BlockingIoController> _logger;

    private readonly IoService _ioService;

    public BlockingIoController(ILogger<BlockingIoController> logger, IoService ioService)
    {
        _logger = logger;
        _ioService = ioService;
    }

    [HttpGet("/blocking/{delay}", Name = "BlockingApi")]
    public IEnumerable<DummyResponse> Get(int delay)
    {
        _logger.LogDebug("calling api with delay {}", delay);
        var response1 = _ioService.HandleIo(delay);
        var result = _ioService.HandleDependentIo(delay, response1);
        return result;
    }
}