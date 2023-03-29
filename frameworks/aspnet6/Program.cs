using aspnet6.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddScoped<IoService>();
builder.Services.AddControllers();

var app = builder.Build();

app.MapControllers();
app.Run();