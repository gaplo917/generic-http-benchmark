namespace aspnet6.Services;

public class IoService
{
    public DummyResponse HandleIo(int delay)
    {
        Thread.Sleep(delay);
        return DummyResponse.Dummy();
    }

    public List<DummyResponse> HandleDependentIo(int delay, DummyResponse response)
    {
        Thread.Sleep(delay);
        return new List<DummyResponse> { response, DummyResponse.Dummy() };
    }

    public async Task<DummyResponse> HandleIoAsync(int delay)
    {
        await Task.Delay(delay);
        return DummyResponse.Dummy();
    }

    public async Task<List<DummyResponse>> HandleDependentIoAsync(int delay, DummyResponse response)
    {
        await Task.Delay(delay);
        return new List<DummyResponse> { response, DummyResponse.Dummy() };
    }
}