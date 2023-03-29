
using aspnet6.Services;
using Microsoft.AspNetCore.Mvc;

namespace aspnet6.Controllers;

[ApiController]
[Route("[controller]")]
public class NonBlockingIoController : ControllerBase
{

    private readonly ILogger<NonBlockingIoController> _logger;

    private readonly IoService _ioService;

    public NonBlockingIoController(ILogger<NonBlockingIoController> logger, IoService ioService)
    {
        _logger = logger;
        _ioService = ioService;
    }

    [HttpGet("/non-blocking/{delay}", Name = "NonBlockingApi")]
    public async Task<IEnumerable<DummyResponse>> Get(int delay)
    {
        _logger.LogDebug("calling api with delay {}", delay);
        var response1 = await _ioService.HandleIoAsync(delay);
        var result = await _ioService.HandleDependentIoAsync(delay, response1);
        return result;
    }
}
