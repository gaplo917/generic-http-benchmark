import { Controller, Get, Param } from '@nestjs/common';
import { AppService } from './app.service';
import { dummyResponse, DummyResponse } from './data/BenchmarkData';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  async nonBlockingIO(ioDelay: number): Promise<DummyResponse> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(dummyResponse());
      }, ioDelay);
    });
  }

  async dependentNonBlockingIO(
    ioDelay: number,
    resp: DummyResponse,
  ): Promise<Array<DummyResponse>> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([resp, dummyResponse()]);
      }, ioDelay);
    });
  }

  @Get('/nestjs-non-blocking/:ioDelay')
  async nonBlockingApi(
    @Param('ioDelay') ioDelay: number,
  ): Promise<Array<DummyResponse>> {
    const resp = await this.nonBlockingIO(ioDelay);
    return await this.dependentNonBlockingIO(ioDelay, resp);
  }
}
