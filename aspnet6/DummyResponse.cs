using System.Runtime.CompilerServices;

namespace aspnet6;

public readonly record struct DummyResponse(
    String? Name, 
    Boolean? IsActive, 
    int? ThreadGroupActiveCount, 
    int? ThreadGroupCount)
{
    public static DummyResponse Dummy(Thread? thread = null)
    {
        return new DummyResponse();
    }
}