FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS base
WORKDIR /app
EXPOSE 8080

FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src
COPY ["aspnet6.csproj", "./"]
RUN dotnet restore "aspnet6.csproj"
COPY . .
WORKDIR "/src/"
RUN dotnet build "aspnet6.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "aspnet6.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "aspnet6.dll"]
